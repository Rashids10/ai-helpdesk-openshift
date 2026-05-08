import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface LoginRequest {
  userEmail: string;
  password: string;
}

export interface TicketCreateRequest {
  title: string;
  description: string;
}

export interface TicketListItem {
  id: string;
  title: string;
  description: string;
  statusKey: 'open' | 'in_progress' | 'closed' | 'unknown';
  statusLabel: string;
  statusTone: 'success' | 'warning' | 'neutral';
  createdAtLabel: string;
  createdAtTimestamp: number;
}

export interface TicketListResult {
  response: Response;
  body: unknown;
  tickets: TicketListItem[];
}

export interface ApiResponseBody {
  accessToken?: string;
  token?: string;
  jwt?: string;
  message?: string;
  [key: string]: unknown;
}

const API_BASE_URL = 'http://localhost:8089/api';
const LOGIN_ENDPOINT = `${API_BASE_URL}/auth/login`;
const LOGGED_IN_USERNAME_ENDPOINT = `${API_BASE_URL}/auth/logged-in-username`;
const TICKET_CREATE_ENDPOINT = `${API_BASE_URL}/ticket/createTicket`;
const MY_TICKETS_ENDPOINT = `${API_BASE_URL}/ticket/my-tickets`;
const REQUEST_TIMEOUT_MS = 5000;

@Injectable({
  providedIn: 'root',
})
export class HelpdeskApiService {
  private readonly ticketsChangedSubject = new Subject<void>();
  readonly ticketsChanged$ = this.ticketsChangedSubject.asObservable();

  async login(payload: LoginRequest): Promise<{ response: Response; body: ApiResponseBody | null }> {
    const result = await this.postJson(LOGIN_ENDPOINT, payload, false);

    return result;
  }

  async getLoggedInUsername(): Promise<string> {
    const response = await this.getText(LOGGED_IN_USERNAME_ENDPOINT, true);

    if (!response.response.ok) {
      throw new Error('Could not load username.');
    }

    return response.body.trim();
  }

  async createTicket(
    payload: TicketCreateRequest,
  ): Promise<{ response: Response; body: ApiResponseBody | null }> {
    return this.postJson(TICKET_CREATE_ENDPOINT, payload, true);
  }

  async getMyTickets(): Promise<TicketListResult> {
    const { response, body } = await this.getJson(MY_TICKETS_ENDPOINT, true);

    return {
      response,
      body,
      tickets: response.ok ? this.normalizeTickets(body) : [],
    };
  }

  notifyTicketsChanged(): void {
    this.ticketsChangedSubject.next();
  }

  storeToken(body: ApiResponseBody | null): string | null {
    const token = this.extractToken(body);

    if (token) {
      localStorage.setItem('accessToken', token);
    }

    return token;
  }

  buildAuthHeaders(): Record<string, string> {
    const headers: Record<string, string> = {};
    const token = this.getAccessToken();

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    return headers;
  }

  extractToken(body: ApiResponseBody | null): string | null {
    const token = body?.accessToken ?? body?.token ?? body?.jwt;

    return typeof token === 'string' && token.trim() ? token : null;
  }

  extractErrorMessage(error: unknown, fallback: string): string {
    if (!error || typeof error !== 'object') {
      return fallback;
    }

    const typedError = error as {
      error?: { message?: unknown } | unknown;
      message?: unknown;
    };

    if (typedError.error && typeof typedError.error === 'object') {
      const backendMessage = (typedError.error as { message?: unknown }).message;
      if (typeof backendMessage === 'string' && backendMessage.trim()) {
        return backendMessage;
      }
    }

    if (typeof typedError.message === 'string' && typedError.message.trim()) {
      return typedError.message;
    }

    if (typeof (error as ApiResponseBody).message === 'string') {
      const backendMessage = (error as ApiResponseBody).message;
      if (backendMessage?.trim()) {
        return backendMessage;
      }
    }

    return fallback;
  }

  private async postJson(
    url: string,
    payload: object,
    includeAuth: boolean,
  ): Promise<{ response: Response; body: ApiResponseBody | null }> {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(includeAuth ? this.buildAuthHeaders() : {}),
        },
        body: JSON.stringify(payload),
        signal: controller.signal,
      });

      const body = await this.safeJson(response);

      return { response, body };
    } finally {
      clearTimeout(timeoutId);
    }
  }

  private async getJson(
    url: string,
    includeAuth: boolean,
  ): Promise<{ response: Response; body: unknown }> {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

    try {
      const response = await fetch(url, {
        method: 'GET',
        headers: includeAuth ? this.buildAuthHeaders() : undefined,
        signal: controller.signal,
      });

      const body = await this.safeUnknownJson(response);

      return { response, body };
    } finally {
      clearTimeout(timeoutId);
    }
  }

  private async getText(
    url: string,
    includeAuth: boolean,
  ): Promise<{ response: Response; body: string }> {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

    try {
      const response = await fetch(url, {
        method: 'GET',
        headers: includeAuth ? this.buildAuthHeaders() : undefined,
        signal: controller.signal,
      });

      const body = await response.text();

      return { response, body };
    } finally {
      clearTimeout(timeoutId);
    }
  }

  private async safeJson(response: Response): Promise<ApiResponseBody | null> {
    try {
      return (await response.json()) as ApiResponseBody;
    } catch {
      return null;
    }
  }

  private async safeUnknownJson(response: Response): Promise<unknown> {
    try {
      return await response.json();
    } catch {
      return null;
    }
  }

  private normalizeTickets(body: unknown): TicketListItem[] {
    if (!Array.isArray(body)) {
      return [];
    }

    return body
      .map((item) => this.normalizeTicket(item))
      .filter((ticket): ticket is TicketListItem => ticket !== null)
      .sort((left, right) => right.createdAtTimestamp - left.createdAtTimestamp);
  }

  private normalizeTicket(item: unknown): TicketListItem | null {
    if (!item || typeof item !== 'object') {
      return null;
    }

    const record = item as Record<string, unknown>;
    const title = this.asString(record['title']) || 'Untitled ticket';
    const description = this.asString(record['description']) || '';
    const statusValue = this.asString(
      record['ticketStatus'] ?? record['ticket_status'] ?? record['status'],
    ).toUpperCase();
    const idValue = this.asString(record['id'] ?? record['ticket']);
    const createdAtRaw = this.asString(
      record['createdAt'] ?? record['created_at'] ?? record['createdDate'],
    );
    const createdAt = this.formatDate(createdAtRaw);

    return {
      id: idValue || `${title}-${createdAt.timestamp}`,
      title,
      description,
      statusKey: this.statusKeyFromValue(statusValue),
      statusLabel: this.statusLabelFromValue(statusValue),
      statusTone: this.statusToneFromValue(statusValue),
      createdAtLabel: createdAt.label,
      createdAtTimestamp: createdAt.timestamp,
    };
  }

  private statusKeyFromValue(value: string): TicketListItem['statusKey'] {
    switch (value) {
      case 'OPEN':
        return 'open';
      case 'IN_PROGRESS':
        return 'in_progress';
      case 'CLOSED':
        return 'closed';
      default:
        return 'unknown';
    }
  }

  private statusLabelFromValue(value: string): string {
    switch (value) {
      case 'OPEN':
        return 'Open';
      case 'IN_PROGRESS':
        return 'In Progress';
      case 'CLOSED':
        return 'Closed';
      default:
        return value ? value.replace(/_/g, ' ') : 'Unknown';
    }
  }

  private statusToneFromValue(value: string): TicketListItem['statusTone'] {
    switch (value) {
      case 'OPEN':
        return 'warning';
      case 'IN_PROGRESS':
        return 'neutral';
      case 'CLOSED':
        return 'success';
      default:
        return 'neutral';
    }
  }

  private formatDate(value: string): { label: string; timestamp: number } {
    if (!value) {
      return { label: 'Unknown date', timestamp: 0 };
    }

    const parsedDate = new Date(value);

    if (!Number.isNaN(parsedDate.getTime())) {
      return {
        label: value.includes('T') ? value.slice(0, 10) : parsedDate.toISOString().slice(0, 10),
        timestamp: parsedDate.getTime(),
      };
    }

    return { label: value, timestamp: 0 };
  }

  private asString(value: unknown): string {
    return typeof value === 'string' ? value.trim() : '';
  }

  private getAccessToken(): string | null {
    const token = localStorage.getItem('accessToken') ?? localStorage.getItem('token') ?? localStorage.getItem('jwt');

    return typeof token === 'string' && token.trim() ? token : null;
  }

}

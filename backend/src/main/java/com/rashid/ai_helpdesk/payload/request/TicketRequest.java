package com.rashid.ai_helpdesk.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TicketRequest {

    @NotBlank
    @Size(min = 3, max = 255)
    private String title;

    @NotBlank
    private String description;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}

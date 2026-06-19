FROM pgvector/pgvector:pg15

USER root

RUN mkdir -p /var/lib/postgresql/data /var/run/postgresql \
    && chgrp -R 0 /var/lib/postgresql /var/run/postgresql \
    && chmod -R g=u /var/lib/postgresql /var/run/postgresql

USER 999

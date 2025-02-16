FROM docker.io/library/mongo:8.0-rc

WORKDIR /server

COPY --chown=mongodb:mongodb .env mongo/mongo-initialize.sh mongo/mongo-replica.key ./

RUN bash -c "set -a && source .env && set +a"

RUN chmod +x mongo-initialize.sh && chmod 400 mongo-replica.key

ENTRYPOINT ["/server/mongo-initialize.sh"]

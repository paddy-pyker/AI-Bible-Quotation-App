#!/bin/bash

DB_DIR="/data/db"

terminate() {
  echo "stopping MongoDB..."
  mongod --shutdown --dbpath $DB_DIR
  exit 0
}

# Trap SIGTERM and forward to the terminate function
trap terminate SIGTERM


# Start MongoDB without --auth
start_mongodb_no_auth() {
  echo "Starting MongoDB without authentication..."
  mongod --bind_ip_all --port "${MONGODB_PORT}" --replSet rs0 --keyFile /server/mongo-replica.key --fork --logpath /tmp/mongod.log --dbpath $DB_DIR
}

# Wait for MongoDB to start
wait_for_mongodb_startup() {
  echo "Waiting for MongoDB to start without authentication..."
  until mongosh --port "${MONGODB_PORT}" --eval 'quit(db.runCommand({ ping: 1 }).ok ? 0 : 2)' &>/dev/null; do
    echo ' . '
    sleep 1
  done
  echo -e "\nMongoDB started."
}

# Initiate the replica set
initiate_replica_set() {
  echo "Initiating replica set..."
  mongosh --port "${MONGODB_PORT}" <<EOF
use admin

var config = {
    "_id": "rs0",
    "version": 1,
    "members": [
        {
            "_id": 1,
            "host": "${MONGODB_HOST}:${MONGODB_PORT}"
        }
    ]
};

rs.initiate(config);
EOF
}



# Wait for PRIMARY to be elected
wait_for_primary() {
  echo "Waiting for PRIMARY to be elected..."
  until mongosh --port "${MONGODB_PORT}" --eval 'rs.status().members.filter(m => m.stateStr === "PRIMARY").length > 0' &>/dev/null; do
    echo ' . '
    sleep 1
  done
  echo -e "\nPRIMARY elected."
}

# Create the initial user
create_initial_user() {
  echo "Creating initial user..."
  mongosh --port "${MONGODB_PORT}" <<EOF
use admin
db.createUser({
  user: "$MONGO_INITDB_ROOT_USERNAME",
  pwd: "$MONGO_INITDB_ROOT_PASSWORD",
  roles: ["root"]
})
EOF
}

# Start MongoDB with authentication
start_mongodb_with_auth() {

  echo "Starting MongoDB with authentication..."
  mongod --bind_ip_all --port "${MONGODB_PORT}" --replSet rs0 --auth --keyFile /server/mongo-replica.key --fork --logpath /tmp/mongod.log --dbpath $DB_DIR
}

# Restart MongoDB with authentication
restart_mongodb_with_auth() {
  echo "Stopping MongoDB to restart with authentication..."
  mongod --shutdown --dbpath $DB_DIR

  echo "Restarting MongoDB with authentication..."
  mongod --bind_ip_all --port "${MONGODB_PORT}" --replSet rs0 --auth --keyFile /server/mongo-replica.key --fork --logpath /tmp/mongod.log --dbpath $DB_DIR
}

# Wait for MongoDB to start with authentication
wait_for_mongodb_with_auth() {
  echo "Waiting for MongoDB to start with authentication..."
  until mongosh --port "${MONGODB_PORT}" -u $MONGO_INITDB_ROOT_USERNAME -p $MONGO_INITDB_ROOT_PASSWORD --authenticationDatabase admin --eval 'quit(db.runCommand({ ping: 1 }).ok ? 0 : 2)' &>/dev/null; do
    echo ' . '
    sleep 1
  done
  echo -e "\nMongoDB with authentication started."
}

# Verify replica set status
verify_replica_set_status() {
  echo "Verifying replica set status..."
  mongosh --port "${MONGODB_PORT}" -u $MONGO_INITDB_ROOT_USERNAME -p $MONGO_INITDB_ROOT_PASSWORD --authenticationDatabase admin <<EOF
use admin
rs.conf();
rs.status();
EOF
}


first_time_run(){

  echo "initializing mongo for first time"
  start_mongodb_no_auth
  wait_for_mongodb_startup
  initiate_replica_set
  wait_for_primary
  create_initial_user
  restart_mongodb_with_auth
  wait_for_mongodb_with_auth
  verify_replica_set_status

}

subsequent_runs(){

  echo "Using pre-existing data from $DB_DIR . . ."
  start_mongodb_with_auth
  wait_for_mongodb_with_auth
  verify_replica_set_status

}




# Main script execution
main() {

  # Check if the $DB_DIR directory is empty
  if [ -z "$(ls -A "$DB_DIR" 2>/dev/null)" ]; then

      first_time_run
      
  else
      subsequent_runs

  fi

  echo "Setup complete. Tailing MongoDB logs..."
  nohup tail -f /tmp/mongod.log &
  wait
}

main



#!/bin/sh
# Start/stop/restart the PostgresSQL server:

case $1 in
  start)
    su postgres -c "/usr/bin/postmaster -i -D /var/lib/pgsql" &
    ;;
  restart)
    kill `cat /var/lib/pgsql/postmaster.pid | head -n 1`
    su postgres -c "/usr/bin/postmaster -i -D /var/lib/pgsql" &
    ;;
  stop)
    kill `cat /var/lib/pgsql/postmaster.pid | head -n 1`
    ;;
  *)
    echo "usage $0 start|stop|restart"
esac

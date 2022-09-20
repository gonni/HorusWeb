sbt clean assembly
docker build --tag horus.web:0.1 .
docker run -p 18090:8090 -d --name horus.web --net horus_net horus.web:0.1
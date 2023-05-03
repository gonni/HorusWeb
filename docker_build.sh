sbt clean assembly
docker build --tag horus.web:0.5 .
docker run -p 18090:8090 -d --name horus.web5 --net horus_net horus.web:0.5

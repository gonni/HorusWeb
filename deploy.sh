sbt -Djavacpp.platform=macosx-x86_64 clean assembly
scp ./horus5.jar horus@studio:/Users/horus/dev/docker-runtime/horus-web/HorusWeb
FROM bellsoft/liberica-openjdk-alpine:17.0.4.1-1
ADD ../build/libs/stand-0.0.1-SNAPSHOT.jar app.jar
ADD ../collections/collection.tsv data.tsv
ADD ../models/bert.pt models/bert.pt
ENTRYPOINT ["java", "-Dspring.profiles.active=standalone", "-jar", "/app.jar", "save-in-batch", "-m=CUSTOM", "--with-id", "/data.tsv"]

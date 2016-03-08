export PROJECT=cod4weaponedit
export PROJECT_VERSION=$(cat pom.xml| grep version | head -n 1 | cut -d ">" -f 2 | cut -d "<" -f 1)
mvn clean package
rm ./target/${PROJECT}-${PROJECT_VERSION}.jar
mkdir ./target/${PROJECT}-${PROJECT_VERSION}
mv ./target/original-${PROJECT}-${PROJECT_VERSION}.jar ./target/${PROJECT}-${PROJECT_VERSION}/${PROJECT}-${PROJECT_VERSION}.jar
mv ./target/lib ./target/${PROJECT}-${PROJECT_VERSION}/
mv ./target/run.bat ./target/${PROJECT}-${PROJECT_VERSION}/
mv ./target/run.sh ./target/${PROJECT}-${PROJECT_VERSION}/
cd target
zip -r ${PROJECT}-${PROJECT_VERSION}.zip ${PROJECT}-${PROJECT_VERSION}


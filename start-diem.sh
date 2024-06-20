ps -elf |grep tron-server|grep -v grep |head -n 1 |awk '{printf $4}'| xargs kill -9
git pull origin master
mvn install
nohup java -Xmx6g -jar target/tron-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=diem >/dev/null 2>log &

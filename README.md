# Build java subscriber
*assuming maven is installed (apt install maven)
(helpful guide with using maven: https://spring.io/guides/gs/maven/)

pom.xml has jeromq as a dependency. Therefore running this maven command should download jeromq (java implementation of zeromq)
```
mvn package
```
The resulting jar file `subscriber-0.1.0.jar` can now be run.

# Installing cppzmq
https://github.com/zeromq/cppzmq
1. Build libzmq via cmake
```
git clone https://github.com/zeromq/libzmq.git
cd libzmq
mkdir build
cd build
cmake ..
sudo make -j4 install
cd ../..
```
2. Build cppzmq via cmake
```
git clone https://github.com/zeromq/cppzmq.git
mkdir build
cd build
cmake ..
sudo make -j4 install
cd ../..
```


# Running publisher/receiver

## Terminal #1: Publisher (c++)
```
g++ publisher.cpp -o publisher -lzmq
./publisher
```

## Terminal #2: Receiver (java)
```
java -jar target/subscriber-0.1.0.jar
```
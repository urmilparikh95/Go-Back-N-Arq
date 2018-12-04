all:
	javac Server.java
	javac Client.java
server:
	javac Server.java
client:
	javac Client.java
clean:
	rm -rf *.class
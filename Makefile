SlidePuzzle.class: SlidePuzzle.java
	javac SlidePuzzle.java
run: SlidePuzzle.class
	java SlidePuzzle < in.txt
log: SlidePuzzle.class
	java SlidePuzzle < in.txt 2> log.txt
null: SlidePuzzle.class
	java SlidePuzzle < in.txt 2> /dev/null
clean: 
	-rm *.class
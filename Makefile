SlidePuzzle.class: SlidePuzzle.java
	javac SlidePuzzle.java
run: SlidePuzzle.class
	java SlidePuzzle
log: SlidePuzzle.class
	java SlidePuzzle 2> log.txt
null: SlidePuzzle.class
	java SlidePuzzle 2> /dev/null
clean: 
	-rm *.class
out: SlidePuzzle.class
	java SlidePuzzle > out.txt
csv: SlidePuzzle.class
	java SlidePuzzle >> out.csv 2> /dev/null
Author(s)
==================
William Howard - http://william.howard.name

Description
==================
This project is a Java6 implementation of the Vector Space Model approach
to query based document retrieval. This project took approximately 21 solid
hours to complete over the planning, coding and testing stages. Testing and 
Analysis was completed using a recent database dump of the wikipedia article 
pages content. The algorithm implemented was described in the 2004 book 
“Information Retrieval: Algorithms and Heuristics Second Edition” written by 
David A. Grossman and Ophir Frieder published by Springer.

This search system was designed to only work on ASCII text files. Some features 
such as serialization have been left out of the implementation but still have 
TODO annotations where they would become relevant.

Potential Reﬁnements
  • Serialization: Posting Lists (the list part of the index) would be able
  to serialize themselves compactly to the HDD when memory availability
  becomes low and they are not required.

  • Time based re-indexing: Indices on documents may become old and
  ﬁles may be modiﬁed after indexing has taken place. Therefore it is pru-
  dent to have time interval based checking of indexed ﬁles to determine if
  the document has been modiﬁed and if so re-index it.

  • Use of language models: Certain terms may not be necessary in the
  index, some terms may be equivalent. Small words, such as many con-
  junctions in the english language (a, or, etc.) can be eliminated entirely
  from the index as their values are almost never relevant. Other words
  may be synonyms of each other or have the same base word, these words
  are technically equivalent but in the index they are not, for example: the
  words ’index’ and ’indicies’ are equivalent as one is the plural of the other
  but they are evaluated diﬀerently.

  • Translation into other languages: While java is a very robust language
  it comes with several disadvantages. In problems such as these memory
  becomes very precious. Since all Java classes extend the java class Object
  there is a distinct overhead for using a class where it is not necessary and
  potentially replacable with much more memory efficient C structs. In fact,
  there is an estimated 8-byte overhead for any Java class and some Collec-
  tions objects are estimated at upwards of 80-bytes without any elements
  inserted. In the case of the document-id/term-frequency mapping for the
  posting lists this becomes a very serious issue. With tens of thousands of
  instantiations of the Map.Entry class to join these terms together, each in-
  cluding the 8-byte overhead for extending java.lang.Object, memory is
  ﬁlled at an alarming rate, especially when compared to C which could in-
  stantiate the same structure for the totality of 6-bytes of memory (uint32
  and uint8).

Installation
==================
No installation is required.  The Java6 JRE is required to be installed before
executing, which can be downloaded at http://java.sun.com

The program should be able to execute without issues on any Java supported 
distribution of Microsoft Windows, GNU/Linux (which has a windowing system),
and Mac OSX (untested).

To run the program execute either run.bat or run.sh depending on whether you
are on Microsoft Windows or GNU/Linux.  

To run on Mac OSX run from the command line the Java class exec.IndexGUI taking 
care to include both jar files as well as the class files in the Java classpath.

License
==================
The source code of this project is licensed under the GPL v3.0 whose text is 
included as gpl-3.0.txt.

JGoodies libraries are licensed under their own licence, included whose text is
include as LICENSE.JGoodies.txt

Libraries
==================
    JGoodies Forms v1.2.1 (http://www.jgoodies.com/)
	Allows much easier creation of form style Swing user interfaces.

    JGoodies Looks v2.2.1 (http://www.jgoodies.com/)
	Gives a much nicer look and feel to Swing user interfaces.
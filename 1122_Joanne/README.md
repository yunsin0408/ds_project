
## First part of Stage 1

It's the first part of stage 1, without Youtube website(transcript). This program automatically fetches multiple web pages, calculates text relevance scores based on predefined keywords and weights, and outputs the ranked results. 
##

 The list of URLs and keyword weights are written in `Main.java`, and can be modified in the code if needed.

 ## Keywords, weight and websites

* ISO *4
* Standard *3
* Sustain *2
* Certificate *1

- https://www.iso.org/home.html
- https://en.wikipedia.org/wiki/International_Organization_for_Standardization
- https://www.cyberark.com/what-is/iso/




## How to Run

1. Make sure you have Java installed (JDK 8 or above).
2. Compile the project:

```
javac Main.java
```

3. Execute:

```
java Main
```

4. The program will download the pages, calculate the scores, and print results in the console.
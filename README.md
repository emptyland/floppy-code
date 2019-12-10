# Floppy Code

![Raw Ring Code](src/test/resources/4.png)
![Colored Ring Code](src/test/resources/3.png)

Yet another QR code.

## How to generate Ring Code picture

### Build java code

```bash
$ cd project-directory
$ ./build.sh
```

### Use Ring Code Utils

jar file path:

`target/ring-code-utils-jar-with-dependencies.jar`

```bash
$ java -jar target/ring-code-utils-jar-with-dependencies.jar
```

```
usage: java -jar ring-code-utils.jar [OPTION] <FILENAME>
 -c         🌈 Colored ring code picture. Require with -g.
 -f <arg>   📄 Ring code picture format. Require with -g.
 -g <arg>   🏞 Generate ring code picture.
 -help      😳 Help information.
 -p         📷 Read from ring code in photo.
 -r         📤 Read the raw ring code picture.
 -test      🛠 Debug mode.
```




# IntelliJ IDEA with BSP

First, delete all the artifact directories:

```
rm -rf .bsp .bloop .idea
```

Then run bloopInstall:

```
sbt bloopInstall
```

This will create the bsp config.

After that, then open the project in IntelliJ IDEA, and select it as a BSP project.

Turn on "build automatically on file save."

Turn off "Automatically show first error in editor."



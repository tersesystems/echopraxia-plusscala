# IntelliJ IDEA with BSP

Running with Scala 3 and Scala 2 makes IntelliJ IDEA very confused, so you pretty much have to use BSP to sort it out.

Unfortunately, the bloop server in IntelliJ, Metals, and SBT all have to work it out.  If you have bloop running in the background through coursier, everything is hopelessly confused and changes in sbt won't be picked up, etc.

## Installing

First, delete all the artifact directories:

```
rm -rf .bsp .bloop .idea
```

Then run bloopInstall:

```
sbt bloopInstall
```

This will create the bsp config.

### Start sbt and bloop

First, open up a console in SBT.

```
sbt
```

This will start a bloop server.  Do not exit sbt, IntelliJ IDEA will get confused.

### Start up IntelliJ IDEA

After that, then open the project in IntelliJ IDEA.  It will show you the "create project from existing sources" dialog, and you can then select it as a BSP project.

Turn on "build automatically on file save."  This will cause SBT to kick off.

Turn off "Automatically show first error in editor."  You don't want to jump around the project.

If you are using Gateway, close and stop the project after you leave.

### Start up Visual Studio Code

The really nice thing about BSP is that you can have several editors working at the same time.  This helps when one of them bugs out or is displaying incorrect output.

I like to use VS Code for Scala 3 editing, and IntelliJ for running the tests.

Set the metals [java-home variable](https://scalameta.org/metals/docs/editors/vscode) to SDK current.

```
/home/wsargent/.sdkman/candidates/java/current
```

#### Running Tests and Main in VS Code

If VS Code doesn't show you the lense to run a test, you can use the command palete and run `metals.test-current-file` from there.

If you want to run `Main` then use `metals.run-current-file.

If you want to run all tests, use `metals.test-current-target.

The "Testing" tab in the left will not show available tests, but it will show tests that have already run.  I'm not sure why it doesn't pick up Scalatest automatically.

#### Running Scalafix in VS Code

You can run Scalafix from the command palette using `metals.scalafix-run`.


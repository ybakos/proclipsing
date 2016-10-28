# Proclipsing

## [Eclipse](http://eclipse.org) Plugin for [Processing](http://processing.org)

**Note:** This repository was imported from the original [SVN repository on Google Code](https://code.google.com/p/proclipsing/) on Nov. 16, 2014. It is the current "best known" project that maintains this plugin. Yes, there are still some flaws.

**Tip:** Have you considered using [IntelliJ IDEA](https://www.jetbrains.com/idea/)? See [Using Processing with IntelliJ](https://vimeo.com/155012427).

# Overview

The purpose of Proclipsing is to lower the difficulty of using Processing in Eclipse.

This Eclipse plugin bootstraps the creation of an Eclipse Java project that relies on the Processing **.jar** dependencies. This is different from the [processing-eclipse](http://github.com/processing/processing-eclipse) experiment, which adheres to the PDE / codegen approach found in the Processing IDE itself.

It allows you to easily create a Processing project in Eclipse by simply putting in the path to your Processing app, the path to your Sketch folder (for contributed libraries), and then selecting the libraries you want. Proclipsing then creates a project with the desired libs (with native libraries), skeleton package structure, and a PApplet.

You can also add or remove libraries later with an option in the project preferences menu.

## Installing

1. Download a [release](https://github.com/ybakos/proclipsing/releases) **.zip** file.
2. Unzip the file and place the folder in a convenient location.
3. In Eclipse, select the menu item _Help > Install New Software_. In the dialogue that appears, select the _Add..._ button. Enter **Proclipsing** as the _Name:_, and select the _Local..._ button. Navigate to the location of your Proclipsing folder, and within, select the **proclipsingSite** folder and select the _Open_ button.
4. Back in the _Install_ dialogue, select the _Select All_ button, and then select _Next >_.
5. Accept the license agreement and select _Finish_. If you are prompted to restart Eclipse, do it.

Here's an old [Proclipsing Setup Screencast](https://vimeo.com/19076476).

## P5 Exporter

We've also teamed up with Daniel Howe, so you can get his P5 Exporter from the same update site. With P5 Exporter, you can export projects from eclipse similar to the way you do from the Processing IDE.


© 2010, 2016 Proclipsing Team & Yong Joseph Bakos, unless otherwise noted. [See the license](LICENSE.md).

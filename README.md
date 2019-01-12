# TornadoFX-Suite (Stage 1)
Automated UI test generation for TornadoFX.
**Note**:  For the time being, the scope of this project is kept small.  A user should be able to input their TornadoFX project and have tests generated for inputs like textfields, checkboxes, buttons, and so on

![alttext](https://github.com/ahinchman1/TornadoFX-Suite/blob/master/tornadofx-suite.png)

Testing has always been a challenge for engineers - many of us are opiniated on how testing should affect our development process and what ought to be tested in applications. While we will do our best to update as we go both in our issues and on here, TornadoFX-Suite is a WIP which will exist in 3 stages:

* Stage 1: finding ways for Kotlin to create code-that-creates-code (current progress mapped below)
![alttext](https://github.com/ahinchman1/TornadoFX-Suite/blob/master/Generative%20UI%20Testing.png)
* Stage 2: creating a DSL to create an easy structure for writing tests
* Stage 3: using machine-learning to learn what makes more useful testing rules

Interested in the project? It's open source and anyone is welcome to contribute! Feel free to head over to the [Issues page](https://github.com/ahinchman1/TornadoFX-Suite/issues) to see what's going on.

## Collaboration Standards
You'll notice some classifications for the way the issues are labeled.  Ideally, every issue should be formatted a certain way. I just made this up. If you guys have something you like better let me know. You'll often see the issues necessary for this project to move forward in the format of 

`Stage{num}@[category]`:
* `FEATURE`
* `BUG`
* `INVESTIGATION`

Where a `category` is just an arbitary set of concerns [`FEATURE`, `BUG`, `INVESTIGATION`] I made to address different kinds of issues as this project progresses.

And when it comes to naming branches to address these issues:
`bug_[bugNumber]/[name of bug]`
`enhancement_[enhancement]/[name of enhancement]`
and so on.

### Features

[Features](https://github.com/ahinchman1/TornadoFX-Suite/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+FEATURE) are issues that are required to complete a stage.

### Bugs
I like to go with Yegor Bugayenko's definition, as quoted in his [blog](https://www.yegor256.com/2018/02/06/where-to-find-more-bugs.html):

* **Lack of functionality.** If a class ([yegor256/cactoos#558](https://github.com/yegor256/cactoos/issues/558)) or the entire module ([yegor256/cactoos#399](https://github.com/yegor256/cactoos/issues/399)) doesn’t provide the functionality you expect it to have, it’s a bug.

* **Lack of tests.** If a class doesn’t have a unit test ([yegor256/takes#43](https://github.com/yegor256/takes/issues/43)) or the existing test doesn’t cover some critical aspects of the class ([yegor256/cactoos#375](https://github.com/yegor256/cactoos/issues/375)), it’s a bug.

* **Lack of documentation.** If, say, a Javadoc block for a class does not clearly explain to you how to use the class, or the entire module is not documented well ([yegor256/takes#790](https://github.com/yegor256/takes/issues/790)), it’s a bug.

* **Suboptimal implementation.** If a piece of code doesn’t look good to you, and you think it can be refactored to look better, it’s a bug.

* **Design inconsistency.** If the design doesn’t look logical to you ([yegor256/cactoos#436](https://github.com/yegor256/cactoos/issues/436)) and you know how it can be improved, it’s a bug.

* **Naming is weird.** If class, variable or package names don’t look consistent and obvious to you, and you know how they can be fixed ([yegor256/cactoos#274] (https://github.com/yegor256/cactoos/issues/274)), it’s a bug.

* **Unstable test.** If a unit test fails sporadically ([yegor256/takes#506](https://github.com/yegor256/takes/issues/506)) or doesn’t work in some particular environment ([yegor256/jpeek#151](https://github.com/yegor256/jpeek/issues/151])), it’s a bug.

## Investigation
Is there something down the road that might be relevant to later stages? Go ahead in put in a issue for discussion/invesigation.

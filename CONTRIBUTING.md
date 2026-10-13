# Contributing to this project

First of all, thank you for wanting to contribute.

## How to submit bug reports or feature requests
Bug reports or feature requests can be submitted by opening an issue on
<https://github.com/gouttegd/sssom-java/issues>. They may also be sent
by e-mail to <devel@incenp.org>. When using e-mail, please prefix the
subject with `[sssom] `.

## How to submit contributions
Please discuss the change you wish to make first, either through an
issue or by e-mail (see above), before submitting anything.

Contributions can be submitted by opening a pull request on
<https://github.com/gouttegd/sssom-java/pulls>. Pull requests or patches
may also be sent by e-mail to <devel@incenp.org>. As for bug reports or
feature requests, when using e-mail, please prefix the subject with
`[sssom] `.

Please check your contribution meets the following requirements:

* Java code should follow the project’s formatting style. You may use
  this [profile](https://incenp.org/dvlpt/java-style-settings.xml) for
  the Eclipse code formatter, or simply try to mimic the surrounding
  code.
* The test suite (`mvn -Dgpg.skip=true verify`) should still pass with
  your changes.
* Your patches or commits (when submitting pull requests) should each be
  adequately described by a [meaningful commit
  message](https://incenp.org/dvlpt/docs/vcs-good-practices/index.html#sect.commit-messages).

## No “generative AI”
Contributions of any kind (code, documentation, tests…) that have been
fully or partially made by genAI tools are _not_ accepted and any patch
or pull request containing such a contribution will be ignored. Bug
reports or feature requests openly or obviously written by genAI tools
are likely to be ignored as well.

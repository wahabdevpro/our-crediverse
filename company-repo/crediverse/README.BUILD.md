### GitHub Concurrency
The "Crediverse" build occasionally experiences a lengthy queue. This is often a result of various pushes to a pull request (PR) within a single branch. Consequently, a new build, accompanied by tests, is initiated with each push to a PR. Ideally, when a new push is made to the branch while a build is already in progress, the ongoing build should be halted to allow the new one to commence.

Occasionally, a build fails due to unexpected errors resulting from interference between two concurrent builds. This issue arises when two actions are executed simultaneously, and the runner randomly allocates tasks from both actions. To illustrate, suppose the first action encompasses four jobs, and the second action mirrors this setup. The runner might start with the first job of action1, followed by the corresponding job in action2, and so forth. In an ideal scenario, all jobs within action1 should complete before progressing to subsequent tasks.

This identical concern extends to nightly builds. To address these challenges, Github has introduced a concept called "Concurrency," which has been implemented across all workflows within Crediverse, including Credistats and MAS.

Here are the resolutions that have been implemented:

- Whenever a PR is initiated, a build is triggered. In Github's terminology, if you subsequently push another commit to the same PR (termed a "synchronize" event), the prior build is terminated, making way for the new build to take precedence.
- In the event that changes are merged into the main branch while a  main build is ongoing, and additional PRs are merged into the main branch concurrently, the running build is halted to permit the initiation of a new one.
- A similar process is applied to nightly builds. For instance, if both the 1.4 and 1.5 builds are initiated simultaneously, one build will wait until the other has been completed before commencing.

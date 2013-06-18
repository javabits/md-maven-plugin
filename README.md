md-maven-plugin
===============

Markdown maven plugin integration


```cmd
> mvn release:prepare -DscmCommentPrefix="[maven-release-plugin] #XX: " -B -DreleaseVersion=1.0.0.MX -DdevelopmentVersion=1.0-SNAPSHOT
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building Markdown Maven Plugin 1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
....
[INFO] Release preparation complete.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 30.030s
[INFO] Finished at: Tue Jun 18 17:13:13 CEST 2013
[INFO] Final Memory: 10M/361M
[INFO] ------------------------------------------------------------------------
>
>
>
> mvn release:perform
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building Markdown Maven Plugin 1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
.....
[INFO] Cleaning up after release...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 35.615s
[INFO] Finished at: Tue Jun 18 17:14:17 CEST 2013
[INFO] Final Memory: 10M/361M
[INFO] ------------------------------------------------------------------------

```

where: 
 * XX: is the release issue associated to the milestone
 * X: is the milestone number



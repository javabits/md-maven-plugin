private void checkFile(String path) {
    File tableOfContent = new File(basedir, path);
    assert tableOfContent.isFile()
}

checkFile("target/docs/index.html");
checkFile("target/docs/base.css");
checkFile("target/docs/table-content.html");
checkFile("target/docs/icon.gif");
checkFile("target/docs/user-guide/user-guide.html");
checkFile("target/docs/user-guide/image.png");
checkFile("target/default-test-1.0-SNAPSHOT-docs.zip");
checkFile("../../local-repo/org/javabits/maven/md/default-test/1.0-SNAPSHOT/default-test-1.0-SNAPSHOT-docs.zip");

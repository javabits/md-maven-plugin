private void checkFile(String path) {
    File tableOfContent = new File(basedir, path);
    assert tableOfContent.isFile()
}

checkFile("target/docs/index.html");
checkFile("target/docs/toto.css");
checkFile("target/docs/table-content.html");
checkFile("target/options-test-1.0-SNAPSHOT-docs.zip");
checkFile("../../local-repo/org/javabits/maven/md/options-test/1.0-SNAPSHOT/options-test-1.0-SNAPSHOT-docs.zip");
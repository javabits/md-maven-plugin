private void checkFile(String path) {
    File tableOfContent = new File(basedir, path);
    assert tableOfContent.isFile()
}

checkFile("target/site/index.html");
checkFile("target/site/table-content.html");
checkFile("target/site/user-guide/user-guide.html");
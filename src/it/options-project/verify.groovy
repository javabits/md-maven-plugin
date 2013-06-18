private void checkFile(String path) {
    File tableOfContent = new File(basedir, path);
    assert tableOfContent.isFile()
}

checkFile("target/site/index.html");
checkFile("target/site/toto.css");
checkFile("target/site/table-content.html");

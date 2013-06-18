private void checkFile(String path) {
    File tableOfContent = new File(basedir, path);
    assert tableOfContent.isFile()
}

checkFile("target/site/index.html");
checkFile("target/site/base.css");
checkFile("target/site/table-content.html");
checkFile("target/site/icon.gif");
checkFile("target/site/user-guide/user-guide.html");
checkFile("target/site/user-guide/image.png");
[![Travis build status](https://img.shields.io/travis/mike10004/jpeg-segment-finder.svg)](https://travis-ci.org/mike10004/jpeg-segment-finder)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.mike10004/jpeg-segment-finder.svg)](https://repo1.maven.org/maven2/com/github/mike10004/jpeg-segment-finder/)

# jpeg-segment-finder

Java library to determine the offsets and lengths of metadata segments in 
a JPEG file.

## Maven

    <dependency>
        <groupId>com.github.mike10004</groupId>
        <artifactId>jpeg-segment-finder</artifactId>
        <version>0.3</version>
    </dependency>

## Usage

    JpegSegmentFinder finder = new JpegSegmentFinder();
    List<JpegSegmentSpec> segments;
    try (InputStream in = new FileInputStream(jpegFile)) {
        segments = finder.findSegments(in, new IptcReader());
    }
    JpegSegmentSpec iptc = segments.get(0);
    byte[] jpegBytes = Files.readAllBytes(jpegFile.toPath());
    int from = (int) iptc.contentOffset;
    int to = from + (int) iptc.contentLength;
    byte[] segmentBytes = Arrays.copyOfRange(jpegBytes, from, to);
    // ...analyze those segment bytes as you please...

## Credits

Thank you to Drew Noakes for [metadata-extractor][metadata-extractor]. This 
library is a hack on some classes in that library. The test image is from 
that project's test resources.

[metadata-extractor]: https://github.com/drewnoakes/metadata-extractor


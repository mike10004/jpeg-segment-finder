package io.github.mike10004.jpegsegmentfinder;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JpegSegmentFinderExample {

    public static void main(String[] args) throws Exception {
        File jpegFile = new File(JpegSegmentFinderExample.class.getResource("/image-with-iptc-caption.jpg").toURI());
        // Find IPTC segments marked by byte 0xED
        JpegSegmentFinder finder = new JpegSegmentFinder();
        List<JpegSegmentSpec> segments;
        try (InputStream in = new FileInputStream(jpegFile)) {
            segments = finder.findSegments(in, Collections.singleton((byte) 0xED));
        }
        JpegSegmentSpec iptc = segments.get(0);
        byte[] jpegBytes = Files.readAllBytes(jpegFile.toPath());
        int from = (int) iptc.contentOffset;
        int to = from + (int) iptc.contentLength;
        byte[] segmentBytes = Arrays.copyOfRange(jpegBytes, from, to);
        // ...analyze those segments bytes as you please...
        String segmentHex = BaseEncoding.base16().encode(segmentBytes);
        System.out.format("%d bytes of APPD data: %s%n", segmentBytes.length, StringUtils.abbreviate(segmentHex, 64));
    }

}

package io.github.mike10004.jpegsegmentfinder;

import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegSegmentType;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.file.FileMetadataDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.iptc.IptcReader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class JpegSegmentFinderTest {

    private static final boolean dumpMeta = true;
    private static final boolean verbose = false;

    private Metadata dumpMetadata(File imageFile) throws IOException, ImageProcessingException {
        Metadata metadata = com.drew.imaging.ImageMetadataReader.readMetadata(imageFile);
        if (!dumpMeta) {
            return metadata;
        }
        System.out.format("=========== %s (%d bytes) ====================%n", imageFile.getName(), imageFile.length());
        metadata.getDirectories().forEach(directory -> {
            if (!(directory instanceof FileMetadataDirectory)) {
                directory.getTags().forEach(tag -> System.out.format("tag: %s=%s%n", tag.getTagName(), StringUtils.abbreviate(StringEscapeUtils.escapeJava(tag.getDescription()), 64)));
            }
        });
        return metadata;
    }

    @Test
    public void findIptcCaption() throws Exception {
        File imageFile = new File(getClass().getResource("/image-with-iptc-caption.jpg").toURI());
        Metadata originalMetadata = dumpMetadata(imageFile);
        checkState(countErrors(originalMetadata) == 0, "must start out error free");
        checkState(containsIptcCaption(originalMetadata), "this test requires that the input image contains an IPTC Abstract/Caption");
        Set<JpegSegmentType> iptcMarkers = ImmutableSet.copyOf(new IptcReader().getSegmentTypes());
        JpegSegmentFinder finder = new JpegSegmentFinder();
        CountingInputStream inputStream_;
        List<JpegSegmentSpec> specs;
        try (CountingInputStream inputStream = new CountingInputStream(new FileInputStream(imageFile))) {
            inputStream_ = inputStream;
            specs = finder.findSegmentsByType(inputStream, iptcMarkers);
        }
        long bytesRead = inputStream_.getByteCount();
        if (verbose) System.out.format("read %d bytes from %d-byte file%n", bytesRead, imageFile.length());
        if (verbose) specs.forEach(System.out::println);

        JpegSegmentSpec iptcSegment = specs.stream().filter(spec -> spec.marker == JpegSegmentType.APPD.byteValue).findAny().orElse(null);
        assertNotNull(iptcSegment);

        ByteSource iptcSegmentBytes = Files.asByteSource(imageFile).slice(iptcSegment.headerOffset, iptcSegment.fullLength());
        ByteSource segmentHead = iptcSegmentBytes.slice(0, 16);
        ByteSource segmentTail = iptcSegmentBytes.slice(iptcSegmentBytes.size() - 16, 16);
        String segmentHeadHex = BaseEncoding.base16().encode(segmentHead.read());
        String segmentTailHex = BaseEncoding.base16().encode(segmentTail.read());

        if (verbose) System.out.format("%s...%s%n", segmentHeadHex, segmentTailHex);

        // now remove the segment and read the image again
        File iptcFreeFile = File.createTempFile("iptc-free", ".jpg");
        ByteSource modifiedFileHead = Files.asByteSource(imageFile).slice(0, iptcSegment.headerOffset);
        long tailStart = iptcSegment.headerOffset + iptcSegment.fullLength();
        ByteSource modifiedFileTail = Files.asByteSource(imageFile).slice(tailStart, imageFile.length() - tailStart);
        ByteSource.concat(modifiedFileHead, modifiedFileTail).copyTo(Files.asByteSink(iptcFreeFile));
        Metadata modifiedMetadata = dumpMetadata(iptcFreeFile);
        int numErrors = countErrors(modifiedMetadata);
        assertEquals("errors after operation", 0, numErrors);
        assertFalse("contains IPTC caption", containsIptcCaption(modifiedMetadata));
        List<JpegSegmentSpec> segmentsAfterClean;
        try (InputStream in = new FileInputStream(iptcFreeFile)) {
            segmentsAfterClean = finder.findSegmentsByType(in, iptcMarkers);
        }
        assertEquals("expect empty after cleaning", Collections.emptyList(), segmentsAfterClean);
    }

    @Test
    public void findExifSegments() throws Exception {
        File imageFile = new File(getClass().getResource("/image-with-exif.jpg").toURI());
        Set<JpegSegmentType> exifMarkers = ImmutableSet.of(JpegSegmentType.APP1);
        List<JpegSegmentSpec> segments;
        try (InputStream in = new FileInputStream(imageFile)) {
            segments = new JpegSegmentFinder().findSegmentsByType(in, exifMarkers);
        }
        segments.forEach(System.out::println);
        assertNotEquals("expect some exif segments", Collections.emptyList(), segments);
    }

    private static int countErrors(Metadata md) {
        return ImmutableList.copyOf(md.getDirectories()).stream().mapToInt(Directory::getErrorCount).sum();
    }

    private static boolean containsIptcCaption(Metadata md) {
        IptcDirectory directory = md.getFirstDirectoryOfType(IptcDirectory.class);
        return directory != null && directory.containsTag(IptcDirectory.TAG_CAPTION);
    }
}
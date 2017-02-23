package org.chocolateam.hashcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.chocolateam.hashcode.input.Endpoint;
import org.chocolateam.hashcode.input.Latency;
import org.chocolateam.hashcode.input.RequestDesc;
import org.chocolateam.hashcode.input.StreamingProblem;
import org.chocolateam.hashcode.output.Solution;
import org.hildan.hashcode.input.parser.HCParser;
import org.hildan.hashcode.input.parser.readers.TreeObjectReader;

import com.lwouis.hashcode.ProblemSolver;

public class Solver implements ProblemSolver {

    private final HCParser<StreamingProblem> parser;

    public Solver() {
        TreeObjectReader<Latency> latencyReader = TreeObjectReader.of(Latency::new)
                .addFieldsLine("cacheIndex", "latency");

        TreeObjectReader<RequestDesc> requestDescReader = TreeObjectReader.of(RequestDesc::new)
                .addFieldsLine("videoId", "endpointId", "count");

        TreeObjectReader<Endpoint> endpointReader = TreeObjectReader.of(Endpoint::new)
                .addFieldsLine("dcLatency", "nCaches@nCaches")
                .addArray(Endpoint::setLatencies, Latency[]::new, "nCaches", latencyReader);

        TreeObjectReader<StreamingProblem> rootReader = TreeObjectReader.of(StreamingProblem::new)
                .addFieldsLine("nVideos@V", "nEndpoints@E", "nRequestDescriptions@R", "nCaches@C",
                        "cacheSize")
                .addIntArrayLine((p, sizes) -> p.videoSizes = sizes)
                .addArray((sp, arr) -> sp.endpoints = arr, Endpoint[]::new, "E", endpointReader)
                .addArray((sp, arr) -> sp.requestDescs = arr, RequestDesc[]::new, "R", requestDescReader);

        parser = new HCParser<>(rootReader);
    }

    @Override
    public void solve(String inputFile, String outputFile) {
        try {
            StreamingProblem problem = parser.parse(inputFile);
            Solution solution = problem.solve();
            List<String> outputLines = solution.outputLines();
            writeOutput(outputFile, outputLines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeOutput(String outputFilename, List<String> lines) throws IOException {
        Path filePath = Paths.get(outputFilename);
        Path parentDir = filePath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
}

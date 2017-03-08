package org.chocolateam.hashcode;

import org.chocolateam.hashcode.input.Endpoint;
import org.chocolateam.hashcode.input.Latency;
import org.chocolateam.hashcode.input.RequestDesc;
import org.chocolateam.hashcode.input.StreamingProblem;
import org.hildan.hashcode.utils.parser.HCParser;
import org.hildan.hashcode.utils.parser.readers.TreeObjectReader;
import org.hildan.hashcode.utils.runner.HCRunner;
import org.hildan.hashcode.utils.runner.UncaughtExceptionsPolicy;
import org.hildan.hashcode.utils.solver.HCSolver;

public class Main {

    public static void main(String[] args) {
        TreeObjectReader<Latency> latencyReader = TreeObjectReader.of(Latency::new)
                                                                  .fieldsAndVarsLine("cacheIndex", "latency");

        TreeObjectReader<RequestDesc> requestDescReader = TreeObjectReader.of(RequestDesc::new)
                                                                          .fieldsAndVarsLine("videoId", "endpointId", "count");

        TreeObjectReader<Endpoint> endpointReader = TreeObjectReader.of(Endpoint::new)
                                                                    .fieldsAndVarsLine("dcLatency", "nCaches@nCaches")
                                                                    .arraySection(Endpoint::setLatencies, Latency[]::new, "nCaches", latencyReader);

        TreeObjectReader<StreamingProblem> rootReader = TreeObjectReader.of(StreamingProblem::new)
                                                                        .fieldsAndVarsLine("nVideos@V", "nEndpoints@E", "nRequestDescriptions@R", "nCaches@C",
                                                                                "cacheSize")
                                                                        .intArrayLine((p, sizes) -> p.videoSizes = sizes)
                                                                        .arraySection((sp, arr) -> sp.endpoints = arr, Endpoint[]::new, "E", endpointReader)
                                                                        .arraySection((sp, arr) -> sp.requestDescs = arr, RequestDesc[]::new, "R", requestDescReader);

        HCParser<StreamingProblem> parser = new HCParser<>(rootReader);
        HCSolver<StreamingProblem> solver = new HCSolver<>(parser, StreamingProblem::solve);
        HCRunner<String> runner = new HCRunner<>(solver, UncaughtExceptionsPolicy.PRINT_ON_STDERR);
        runner.run(args);
    }
}

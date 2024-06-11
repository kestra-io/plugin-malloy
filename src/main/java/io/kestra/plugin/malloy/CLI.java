package io.kestra.plugin.malloy;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.runners.ScriptService;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.scripts.exec.AbstractExecScript;
import io.kestra.plugin.scripts.exec.scripts.models.DockerOptions;
import io.kestra.plugin.scripts.exec.scripts.models.ScriptOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Execute one or more Malloy commands from the Command Line Interface."
)
@Plugin(examples = {
    @Example(
        full = true,
        title = "Create a Malloy script and run the malloy-cli run command.",
        code = """
               id: malloy
               namespace: dev
               
               tasks:
                 - id: run_malloy
                   type: io.kestra.plugin.malloy.CLI
                   inputFiles:
                     model.malloy: |
                       source: my_model is duckdb.table('https://huggingface.co/datasets/kestra/datasets/raw/main/csv/iris.csv')
                
                       run: my_model -> {
                           group_by: variety
                           aggregate:
                               avg_petal_width is avg(petal_width)
                               avg_petal_length is avg(petal_length)
                               avg_sepal_width is avg(sepal_width)
                               avg_sepal_length is avg(sepal_length)
                       }
                   commands:
                     - malloy-cli run model.malloy
               """
    )
})
public class CLI extends AbstractExecScript {

    private static final String DEFAULT_IMAGE = "ghcr.io/kestra-io/malloy";

    @Schema(
        title = "Docker options when using the `DOCKER` runner."
    )
    @PluginProperty
    @Builder.Default
    protected DockerOptions docker = DockerOptions.builder()
        .image(DEFAULT_IMAGE)
        .build();

    @Schema(
        title = "The commands to run."
    )
    @PluginProperty(dynamic = true)
    protected List<String> commands;

    @Builder.Default
    protected String containerImage = DEFAULT_IMAGE;

    @Override
    public ScriptOutput run(RunContext runContext) throws Exception {
        List<String> commandsArgs = ScriptService.scriptCommands(
            this.interpreter,
            this.getBeforeCommandsWithOptions(),
            this.commands
        );

        return this.commands(runContext)
            .withCommands(commandsArgs)
            .run();
    }
}

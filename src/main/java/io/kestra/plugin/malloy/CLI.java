package io.kestra.plugin.malloy;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.runners.TaskRunner;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.scripts.exec.AbstractExecScript;
import io.kestra.plugin.scripts.exec.scripts.models.DockerOptions;
import io.kestra.plugin.scripts.exec.scripts.models.ScriptOutput;
import io.kestra.plugin.scripts.runner.docker.Docker;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Run Malloy CLI commands",
    description = "Runs malloy-cli commands through the configured Task Runner; defaults to Docker with the `ghcr.io/kestra-io/malloy` image when none is provided."
)
@Plugin(examples = {
    @Example(
        full = true,
        title = "Create a Malloy script and run the malloy-cli run command.",
        code = """
               id: malloy
               namespace: company.team

               tasks:
                 - id: run_malloy
                   type: io.kestra.plugin.malloy.CLI
                   # malloy-cli is present when using the default Docker Task Runner image.
                   # If you override the taskRunner with a non-container runner, ensure malloy-cli is on PATH.
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
    ),
    @Example(
        full = true,
        title = "Run Malloy with local data and a setup step.",
        code = """
               id: malloy_local
               namespace: company.team

               tasks:
                 - id: run_local
                   type: io.kestra.plugin.malloy.CLI
                   beforeCommands:
                     - malloy-cli compile model.malloy
                   inputFiles:
                     model.malloy: |
                       source: iris is csv('data/iris.csv')
                       run: iris -> {
                         group_by: species
                         aggregate: avg_petal_length is avg(petal_length)
                       }
                     data/iris.csv: |
                       sepal_length,sepal_width,petal_length,petal_width,species
                       5.1,3.5,1.4,0.2,setosa
                       6.2,3.4,5.4,2.3,virginica
                       5.9,3.0,4.2,1.5,versicolor
                   commands:
                     - malloy-cli run model.malloy --limit 5
               """
    )
})
public class CLI extends AbstractExecScript implements RunnableTask<ScriptOutput> {

    private static final String DEFAULT_IMAGE = "ghcr.io/kestra-io/malloy";

    @Schema(
        title = "Malloy CLI commands",
        description = "Commands rendered with flow variables and executed in order using the configured interpreter."
    )
    protected Property<List<String>> commands;

    @Schema(
        title = "Task runner",
        description = "Runner used to execute malloy-cli; defaults to Docker unless another Task Runner is provided."
    )
    @PluginProperty
    @Builder.Default
    @Valid
    private TaskRunner<?> taskRunner = Docker.instance();

    @Schema(
        title = "Container image",
        description = "Used only with container-based Task Runners; defaults to `ghcr.io/kestra-io/malloy` when no image is set."
    )
    @Builder.Default
    private Property<String> containerImage = Property.ofValue(DEFAULT_IMAGE);

    @Override
    protected DockerOptions injectDefaults(RunContext runContext, DockerOptions original) throws IllegalVariableEvaluationException {
        var builder = original.toBuilder();
        if (original.getImage() == null) {
            builder.image(runContext.render(this.getContainerImage()).as(String.class).orElse(DEFAULT_IMAGE));
        }

        return builder.build();
    }

    @Override
    public ScriptOutput run(RunContext runContext) throws Exception {
        return this.commands(runContext)
            .withTaskRunner(this.taskRunner)
            .withContainerImage(runContext.render(this.containerImage).as(String.class).orElseThrow())
            .withInterpreter(this.interpreter)
            .withBeforeCommands(this.beforeCommands)
            .withBeforeCommandsWithOptions(true)
            .withCommands(this.commands)
            .run();
    }
}

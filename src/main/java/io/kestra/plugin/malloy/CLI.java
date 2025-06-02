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
    title = "Execute one or more Malloy commands."
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
public class CLI extends AbstractExecScript implements RunnableTask<ScriptOutput> {

    private static final String DEFAULT_IMAGE = "ghcr.io/kestra-io/malloy";

    @Schema(
        title = "The commands to run."
    )
    protected Property<List<String>> commands;

    @Schema(
        title = "The task runner to use.",
        description = "Task runners are provided by plugins, each have their own properties."
    )
    @PluginProperty
    @Builder.Default
    @Valid
    private TaskRunner<?> taskRunner = Docker.instance();

    @Schema(title = "The task runner container image, only used if the task runner is container-based.")
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

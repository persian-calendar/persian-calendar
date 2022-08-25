#!/usr/bin/env kscript

@file:DependsOn("it.krzeminski:github-actions-kotlin-dsl:0.25.0")

import it.krzeminski.githubactions.domain.RunnerType.UbuntuLatest
import it.krzeminski.githubactions.domain.triggers.Push
import it.krzeminski.githubactions.dsl.workflow
import it.krzeminski.githubactions.yaml.writeToFile

val workflow = workflow(
    name = "Notify",
    on = listOf(Push(listOf("main"))),
    sourceFile = __FILE__.toPath(),
) {
    job(id = "step-0", runsOn = UbuntuLatest)
    {
        val template = """
                '.[] | "[\(.id[:8])](\(.url)) • [\(.author.username)](https://github.com/\(.author.username))
                \(.message | gsub("(?<m>[-_*\\[\\]()~>#+=|{}.!`'"'"'])"; "\\\(.m)"))"'
                """.trimIndent()

        val telegramToken = "\${{ secrets.TELEGRAM_TOKEN }}"
        val chatId = "\${{ secrets.TELEGRAM_TOKEN }}"
        run(
            name = "Notify",
            env = linkedMapOf("COMMITS" to "\${{ toJson(github.event.commits) }}"),
            command =
            """
              (
                printenv COMMITS | jq -r "$template"
                printf '📅 @Persian_Calendar'
              ) | \
              jq -R --slurp '{
                text: .,
                disable_web_page_preview: true,
                chat_id: "$chatId",
                parse_mode: "HTML"
              }' | \
              curl -X POST https://api.telegram.org/bot$telegramToken/sendMessage \
                -H 'Content-Type: application/json' \
                --data-binary @-
            """.trimIndent()
        )
    }
}.writeToFile(addConsistencyCheck = false)


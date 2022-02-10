package co.funpeople.db

import com.arangodb.velocypack.*
import com.arangodb.velocypack.module.jdk8.internal.util.JavaTimeUtil
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant

class DbModule : VPackModule {
    override fun <C : VPackSetupContext<C>> setup(context: C) {
        context.registerDeserializer(Instant::class.java) { _, vPack, _ ->
            if (vPack.isString) JavaTimeUtil.parseInstant(vPack.asString).toKotlinInstant() else vPack.asDate.toInstant().toKotlinInstant()
        }

        context.registerSerializer(Instant::class.java) { builder, attribute, value: Instant, _ ->
            builder.add(
                attribute,
                JavaTimeUtil.format(value.toJavaInstant())
            )
        }
    }
}

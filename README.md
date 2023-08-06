# kaphein-java-may-be-value

An anternative to `java.util.Optional` to represent optional fields in classes.  

`MayBeValue` is a container object which may or may not contain a `null`able value.

Unlike `java.util.Optional`, the value described by a `MayBeValue` instance can be `null`. An empty `MayBeValue` instance represents that there is NEITHER non-`null` value nor `null` in the `MayBeValue` instance.  

`MayBeValue` is `java.io.Serializable`. A `boolean` flag which represents whether a value is present or not comes first. If a value is present, the value comes after the flag otherwise `null` comes after the flag.  

`MayBeValue` is intended to represent optional fields in JSON schemas or JavaScript/TypeScript object literals. So, `MayBeValue` can be used as a field or parameter, whereas `java.util.Optional` can't.

## JSON Schema to Java POJO

Here is a JSON schema that has an optional field `baz`. The value of field `baz` can be `true`, `false` or `null`.

```JSON
{
    "foo": 123,
    "bar": "bar",
    "baz": true
}

{
    "foo": 123,
    "bar": "bar",
    "baz": null
}

{
    "foo": 123,
    "bar": "bar",
}
```

The JSON schema can represented like this:

```Java
public class Foo
{
    private Integer foo;

    private String bar;

    private MayBeValue<Boolean> baz;
}
```

## Supported JDK versions

JDK 8 or newer.

## Documentation

Clone this repository, move to the project directory and execute following command in your terminal:

```shell
gradle javadoc
```

The javadoc will be generated in `build/docs/javadoc` directory.

## License

[MIT](./LICENSE)

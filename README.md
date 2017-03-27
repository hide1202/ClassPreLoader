# Class Pre-Loader
- A library that can pre-load classes for that needs to pre-call static initializer.

## Example
### `PreLoadable` annotation
- You can pre-load classes attached `PreLoadable` annotation.
- For example
```java
// You want to pre-load classes that has com.me prefix and that attach PreLoadable annotation
ClassPreLoader loader = new ClassPreLoader("com.me");
loader.loadForPreLoadable();
```

### Custom annotation
- You can pre-load classes attached custom annotation.
- For example
```java
// You want to pre-load classes that has com.me prefix and that attach custom annotation.  
ClassPreLoader loader = new ClassPreLoader("com.me");
loader.load(CustomAnnotation.class);
```
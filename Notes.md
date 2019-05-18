# Notes on AST Parsing

### Generics

Generics will always look like this:
AST Parsing for ItemViewModel<SomeGenericClass>
```$xslt
CallConstructor(type=Simple(pieces=[Piece(name=ItemViewModel, typeParams=[Type(mods=[], ref=Simple(pieces=[Piece(name=CatSchedule, typeParams=[])]))])]), typeArgs=[Type(mods=[], ref=Simple(pieces=[Piece(name=CatSchedule, typeParams=[])]))], args=[], lambda=null)
```


Getting the generic type:
```$xslt
"List<" + node.expr().typeArgs().getObject(0).ref().getType() + ">"
```


##### Difference between Type Parameter and Type Argument

**Type parameter** is blueprint or placeholder for a type declared in generic. 

**Type argument** is actual type used to parametrize generic.
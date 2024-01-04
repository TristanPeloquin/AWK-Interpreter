# Testing code - run through Main with arguments: code.awk text.txt
# Compare console output to given expected output in expected.txt

# Demonstrating loop functionality
BEGIN {
    print("---------------------------")
    print("Test 1: ")
    a["hello"] = "world"
    a[2] = 3
    for(b in a){
        print b
    }
    for(i = 0; i<3; i++){
        print a[i]
    }
    num = 4
    while(num<10){
        print num
        num++
    }
}

#Demonstrating math/logic
BEGIN{
    print("---------------------------")
    print("Test 2: ")
    x = 5 + 2.519
    y = 3 - 5
    z = (10^2) % 51
    if(x == 7.519 && y>=-2 && x>y && z%10 == 9)
        print x, " ", y, " ", z
    if((x+y+z)>1000 || (x^z)>1000)
        print x+y+z, " ", x^z
}

# Demonstrating input processing/conditional blocks
(match($0, "hello")){
    print("---------------------------")
    print("Test 3: ")
    gsub("hello", "TEST")
    gsub("world", "AWK")
    print $0  
}

# Demonstrating input processing/conditional blocks
(!(NR % 2)==0){
    print("---------------------------")
    print("Test 4: ")
    print("Line ", NR, " is an odd line. Here is its contents: ")
    print $0
    split($0, a)
    print("Here are the contents split up: ")
    for(b in a){
        print b
    }
}

# Demonstrating more conditional blocks
(NR>4) {
    print("---------------------------")
    print("Test 5: ")
    print $0
}

# Demonstrating custom functions
END {
    print("---------------------------")
    print("Test 6: ")
    print add("hello", 2, 3)
    print greatestNumber(3,1,2)
    print greatestNumber(5,12,11)
}

function add(a, b, c){
    output = a + b + c
    return output
}

function greatestNumber(a, b, c){
    if(a>b && a>c){
        return a
    }
    else if(b>c){
        return b
    }
    else{
        return c
    }
}
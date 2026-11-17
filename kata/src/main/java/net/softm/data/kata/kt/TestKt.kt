package net.softm.data.kata.kt;
class TestKt {


fun main() {
    printMessage("Hello")
    printMessageWithPrefix("Hello", "Log")
    printMessageWithPrefix("Hello")
    printMessageWithPrefix(prefix = "Log", message = "Hello")
    println(sum(1, 2))
    println(multiply(2, 2))
}

/**
 * printMessage 메소드 생성
 * Unit 은 return 없다는 의미로 생략해도 됨.
 * @param message String 형 파라미터 전달 받음.
 */
fun printMessage(message: String): Unit {
    println(message)
}

/**
 * printMessageWithPrefix 메소드 생성
 * @param message String 형 파라미터 전달 받음.
 * @param prefix String 형 기본값을 "Info"로 선언하고 파라미터
 * prefix 변수에 기본 값이 설정되어 있지만 파라미터로 새로운 값을 전달 받아 변겨아 할 수 있음.
 */
fun printMessageWithPrefix(message: String, prefix: String = "Info") {
    println("[$prefix] $message")
}

/**
 * Int 형 반환 메소드
 * 전달 받은 값 x,y의 합 결과를 Int 형으로 반환
 * @param x Int 형으로 값을 전달 받은
 * @param y Int 형으로 값을 전달 받은
 */
fun sum(x: Int, y: Int): Int {
    return x + y
}

/**
 * Int 형 반환 메소드
 * 전달 받은 값 x,y의 곱한 값 결과를 Int 형으로 반환
 * sum()과 다르게 한줄로 표현 가능.
 */
fun multiply(x: Int, y: Int) = x * y

}
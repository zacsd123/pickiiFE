import XCTest

final class CounterSpikeUITests: XCTestCase {
    func testTappingButtonIncrementsCounter() throws {
        let app = XCUIApplication()
        app.launch()

        let helloText = app.staticTexts["Hello Pickii"]
        XCTAssertTrue(helloText.waitForExistence(timeout: 10), "Hello Pickii 텍스트가 보이지 않음")

        let button = app.buttons["Count: 0"]
        XCTAssertTrue(button.waitForExistence(timeout: 10), "초기 상태 Count: 0 버튼이 보이지 않음")

        button.tap()

        let incremented = app.buttons["Count: 1"]
        XCTAssertTrue(incremented.waitForExistence(timeout: 5), "탭 이후 Count: 1로 바뀌지 않음 — 상태 관리가 동작하지 않을 수 있음")
    }
}

import XCTest

final class SplashScreenUITests: XCTestCase {
    func testSplashScreenShowsThemedContent() throws {
        let app = XCUIApplication()
        app.launch()

        let title = app.staticTexts["Pickii"]
        XCTAssertTrue(title.waitForExistence(timeout: 10), "스플래시 타이틀(Pickii)이 보이지 않음 — MainViewController 진입점 확인 필요")

        let subtitle = app.staticTexts["팀이 필요한 순간, Pickii"]
        XCTAssertTrue(subtitle.waitForExistence(timeout: 5), "스플래시 서브타이틀이 보이지 않음")
    }
}

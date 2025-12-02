from playwright.sync_api import sync_playwright
import os

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()

    # We will open the file directly since we don't have a server running
    # but Playwright might have issues with local file module imports unless we serve it.
    # Actually, module scripts require a server (http:// or https://) to work due to CORS/MIME types.
    # We need to start a simple python http server first.

    # We will assume a server is running on port 8000 for this script
    page.goto("http://localhost:8000/web/index.html")

    # Wait for the page to load
    page.wait_for_load_state("networkidle")

    # Take a screenshot of the initial state
    page.screenshot(path="verification/initial_load.png")

    # Check if header exists
    header = page.locator("header")
    if header.is_visible():
        print("Header is visible")

    # Check if message input exists
    input_box = page.locator("#message-input")
    if input_box.is_visible():
        print("Message input is visible")

    browser.close()

if __name__ == "__main__":
    with sync_playwright() as playwright:
        run(playwright)

/*
 * Copyright © 2015 - 2016 Lightbend, Inc. <http://www.lightbend.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightbend.paradox.markdown

class RefDirectiveSpec extends MarkdownBaseSpec {

  def testMarkdown(text: String, pagePath: String = "page.md", testPath: String = "test.md"): Map[String, String] = markdownPages(
    pagePath -> s"""
      |@@@ index
      |* [test](${Path.basePath(pagePath)}${testPath})
      |@@@
    """,
    testPath -> text
  )

  def testHtml(text: String, pagePath: String = "page.html", testPath: String = "test.html"): Map[String, String] = {
    htmlPages(pagePath -> "", testPath -> text)
  }

  "Ref directive" should "create links with html extension" in {
    testMarkdown("@ref[Page](page.md)") shouldEqual testHtml("""<p><a href="page.html">Page</a></p>""")
  }

  it should "support 'ref:' as an alternative name" in {
    testMarkdown("@ref:[Page](page.md)") shouldEqual testHtml("""<p><a href="page.html">Page</a></p>""")
  }

  it should "handle relative links correctly" in {
    testMarkdown("@ref:[Page](../a/page.md)", pagePath = "a/page.md", testPath = "b/test.md") shouldEqual
      testHtml("""<p><a href="../a/page.html">Page</a></p>""", pagePath = "a/page.html", testPath = "b/test.html")
  }

  it should "handle anchored links correctly" in {
    testMarkdown("@ref:[Page](page.md#header)") shouldEqual testHtml("""<p><a href="page.html#header">Page</a></p>""")
  }

  it should "retain whitespace before or after" in {
    testMarkdown("This @ref:[Page](page.md#header) is linked.") shouldEqual
      testHtml("""<p>This <a href="page.html#header">Page</a> is linked.</p>""")
  }

  it should "parse but ignore directive attributes" in {
    testMarkdown("This @ref:[Page](page.md#header) { .ref a=1 } is linked.") shouldEqual
      testHtml("""<p>This <a href="page.html#header">Page</a> is linked.</p>""")
  }

  it should "throw link exceptions for invalid references" in {
    the[RefDirective.LinkException] thrownBy {
      markdown("@ref[Page](page.md)")
    } should have message "Unknown page [page.html] referenced from [test.html]"
  }

  it should "support referenced links with implicit key" in {
    testMarkdown(
      """This @ref:[Page] { .ref a=1 } is linked.
        |
        |  [Page]: page.md#header
      """.stripMargin) shouldEqual testHtml("""<p>This <a href="page.html#header">Page</a> is linked.</p>""")
  }

  it should "support referenced links with empty key" in {
    testMarkdown(
      """This @ref:[Page][] { .ref a=1 } is linked.
        |
        |  [Page]: page.md#header
      """.stripMargin) shouldEqual testHtml("""<p>This <a href="page.html#header">Page</a> is linked.</p>""")
  }

  it should "support referenced links with defined key" in {
    testMarkdown(
      """This @ref:[Page][123] { .ref a=1 } is linked.
        |
        |  [123]: page.md#header
      """.stripMargin) shouldEqual testHtml("""<p>This <a href="page.html#header">Page</a> is linked.</p>""")
  }

  it should "throw link exceptions for invalid reference keys" in {
    the[RefDirective.LinkException] thrownBy {
      markdown("@ref[Page][123]")
    } should have message "Undefined reference key [123] in [test.html]"
  }
}

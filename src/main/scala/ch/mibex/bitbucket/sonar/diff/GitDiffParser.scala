  case class ExtendedDiffHeader(headerLines: Seq[HeaderLine], index: Option[Index])
  sealed trait Diff
  case class BinaryDiff() extends Diff
  case class GitDiff(gitDiffHeader: FileChange, header: ExtendedDiffHeader, hunks: List[Hunk]) extends Diff {
  def allDiffs: Parser[List[Diff]] = rep1(diff)
  def diff: Parser[Diff] = binaryDiff | gitDiff

  def readUpToNextDiffOrEnd = """(?s).+?(?=((?:diff --git)|$))\n?""".r

  def binaryDiff: Parser[BinaryDiff] = gitDiffHeader ~ extendedDiffHeader ~ "GIT binary patch" ~ readUpToNextDiffOrEnd  ^^ {
    _ => BinaryDiff()
  }

  def gitDiff: Parser[GitDiff] = gitDiffHeader ~ extendedDiffHeader ~ hunks ^^ {
  def hash: Parser[String] = """[0-9a-f]{7,}""".r
    rep(
        oldMode | newMode | deletedFileMode | newFileMode
      | copyFrom | copyTo | renameFrom | renameTo
      | similarityIndex | dissimilarityIndex
    ) ~ opt(index) ^^
  // hunks do not exist when an empty file was added
  def hunks: Parser[List[Hunk]] = opt(unifiedDiffHeader) ~> rep(hunk)
  def lineChange: Parser[LineChange] = ctxLine | addedLine | removedLine | noNewLineAtEOF | newLine
  def noNewLineAtEOF: Parser[CtxLine] = "\\ No newline at end of file" <~ opt(nl) ^^ { l => CtxLine(l) }

  def newLine: Parser[CtxLine] = nl ^^ { l => CtxLine("") }

  def parse(diff: String): Either[ParsingFailure, List[Diff]] = {
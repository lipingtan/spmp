import fs from 'node:fs'
import path from 'node:path'

const rootDir = path.resolve(process.cwd(), 'src/views')
const idKeys = ['communityId', 'buildingId', 'unitId', 'houseId', 'ownerId']
const idPattern = idKeys.join('|')
const forbiddenPattern = new RegExp(
  `<el-input(?:-number)?[^>]*v-model\\s*=\\s*"[^"]*\\.(${idPattern})"`,
  'g'
)

function walk(dir, result = []) {
  const entries = fs.readdirSync(dir, { withFileTypes: true })
  for (const entry of entries) {
    const full = path.join(dir, entry.name)
    if (entry.isDirectory()) {
      walk(full, result)
    } else if (entry.isFile() && full.endsWith('.vue')) {
      result.push(full)
    }
  }
  return result
}

const vueFiles = walk(rootDir)
const violations = []

for (const file of vueFiles) {
  const content = fs.readFileSync(file, 'utf8')
  const matches = [...content.matchAll(forbiddenPattern)]
  if (!matches.length) continue
  for (const match of matches) {
    const key = match[1]
    violations.push({
      file: path.relative(process.cwd(), file).replaceAll('\\', '/'),
      key,
      snippet: match[0]
    })
  }
}

if (violations.length > 0) {
  console.error('检测到基础数据字段使用输入框手填 ID（请改为下拉/级联选择）：\n')
  for (const item of violations) {
    console.error(`- ${item.file} -> ${item.key}`)
    console.error(`  ${item.snippet}`)
  }
  process.exit(1)
}

console.log('通过：未发现基础数据字段手填 ID 的输入框写法。')

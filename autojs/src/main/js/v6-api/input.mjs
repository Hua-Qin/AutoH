import fs from 'fs'
import path from 'node:path'


export async function parseInput() {
    const dirs = fs.readdirSync('./src').filter((name) => {
        if (name == 'inline_modules') {
            return false
        }
        return fs.statSync(path.resolve("src", name)).isDirectory()
    })
    const input = Object.fromEntries(dirs.map(name => [
        path.join(name, 'index'),
        path.resolve("src", name, 'index.ts')
    ]))
    input['init'] = "src/init.ts"
    return input
}
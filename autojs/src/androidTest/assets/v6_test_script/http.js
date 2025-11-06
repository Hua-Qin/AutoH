
let uri = "127.0.0.1:3000"

let res = http.get(uri)
console.assert(res.statusCode == 200)
let data = res.body.json()
console.assert(data.method == "GET")
console.assert(data.pathname == "/")

res = http.postJson(uri, { a: 1, b: 2 })
console.assert(res.statusCode == 200)
data = res.body.json()
console.assert(data.method == "POST")
console.assert(data.pathname == "/")
console.assert(data.body.json.a == 1)
console.assert(data.body.json.b == 2)

res = http.post(uri + "/abc", "datastring",{
    contentType: "text/plain"
})
console.assert(res.statusCode == 200)
data = res.body.json()
console.assert(data.method == "POST")
console.assert(data.pathname == "/abc")
console.assert(data.body.text == "datastring")

res = http.postMultipart(uri,{
    a: "value1",
    b: "value2",
})
console.assert(res.statusCode == 200)
data = res.body.json()
console.assert(data.method == "POST")
console.assert(data.headers['content-type'].startsWith("multipart/form-data;"))
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../env/environment';
import { CAUserDTO } from '../models/ca-user.model';


@Injectable({
  providedIn: 'root'
})
export class UserService {

    constructor(private http: HttpClient) { }
    addCAUser(dto: CAUserDTO): Observable<string> {
      return this.http.post(`${environment.apiHost}/admin/add-ca-user`, dto, { responseType: 'text' });
    }
}
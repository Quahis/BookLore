import {Injectable, inject} from '@angular/core';
import {API_CONFIG} from '../../../core/config/api-config';
import {HttpClient} from '@angular/common/http';
import {AuthService} from '../../../shared/service/auth.service';

@Injectable({
  providedIn: 'root'
})
export class NewPdfReaderService {

  private readonly pagesUrl = `${API_CONFIG.BASE_URL}/api/v1/pdf`;
  private readonly imageUrl = `${API_CONFIG.BASE_URL}/api/v1/media/book`;
  private authService = inject(AuthService);
  private http = inject(HttpClient);

  private getToken(): string | null {
    return this.authService.getOidcAccessToken() || this.authService.getInternalAccessToken();
  }

  private appendToken(url: string): string {
    const token = this.getToken();
    return token ? `${url}${url.includes('?') ? '&' : '?'}token=${token}` : url;
  }

  getAvailablePages(bookId: number) {
    return this.http.get<number[]>(this.appendToken(`${this.pagesUrl}/${bookId}/pages`));
  }

  getPageImageUrl(bookId: number, page: number): string {
    return this.appendToken(`${this.imageUrl}/${bookId}/pdf/pages/${page}`);
  }
}
